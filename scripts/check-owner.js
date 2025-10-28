const { ethers } = require('hardhat')

async function main() {
  const sbtAddress = process.env.SBT_ADDRESS
  const tokenId = Number(process.env.TOKEN_ID || '1')
  if (!sbtAddress) {
    throw new Error('SBT_ADDRESS env var required')
  }
  const sbt = await ethers.getContractAt('MiVotoSoulboundToken', sbtAddress)
  const owner = await sbt.ownerOf(tokenId)
  const balance = await sbt.balanceOf(owner)
  console.log(`ownerOf(${tokenId}) = ${owner}`)
  console.log(`balanceOf(${owner}) = ${balance.toString()}`)
}

main().catch((err) => {
  console.error(err)
  process.exitCode = 1
})
